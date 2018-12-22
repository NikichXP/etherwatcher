package com.nikichxp.ether2j

import com.nikichxp.util.Locks
import kotlinx.coroutines.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Repository
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.protocol.core.methods.response.Transaction
import rx.Subscriber
import java.math.BigInteger
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Logger

@Repository
class EtherBlockExplorer(
        private val etherConfig: EtherConfig,
        private val etherOperations: EtherOperations,
        private val etherWalletHolder: EtherWalletHolder) {

    private var lastObservedBlock: BigInteger = etherConfig.getLastBlock()
    private var inputWalletAddresses: MutableList<String> = etherWalletHolder.listWalletAddresses().toMutableList()
    private val log = Logger.getLogger("ether-scanner")

    // watcher-related data
    private val lastTransactionAnalyzeTime: AtomicLong = AtomicLong(0L)
    private var blockExplorer: Job = GlobalScope.launch {}

    private val etherExploreBlock = AtomicBoolean(false)
    private val proceedAddresses = ConcurrentSkipListSet<String>()

    suspend fun processTransaction(transaction: Transaction) {
        Locks.withLock(transaction.from) {
            etherConfig.onEachTransaction(transaction)
        }
    }

    @Scheduled(fixedDelay = 30_000L)
    fun autoUpdate() {
        if (etherExploreBlock.get()) {
            return
        }
        if (lastTransactionAnalyzeTime.get().plus(40_000) < System.currentTimeMillis()) {
            blockExplorer.cancel()
            blockExplorer = GlobalScope.launch { observeBlocks() }
        }
    }

    fun suspendBlockExplorer() {
        etherExploreBlock.set(true)
        blockExplorer.cancel()
        // fail-safe
        GlobalScope.launch {
            delay(3_000_000)
            etherExploreBlock.set(false)
        }
    }

    fun resumeBlockExplorer() {
        etherExploreBlock.set(false)
    }

    fun getBlockChainStatus(): Long = lastObservedBlock.longValueExact()

    fun blockingTransactionGet(block: Long) {
        etherOperations.web3j.ethGetBlockByNumber({ block.toString() }, true)
                .send()
                .block
                .transactions
                .forEach {
                    it.get()
                }
    }

    private suspend fun observeBlocks() {
        val subscriber = object : Subscriber<Transaction>() {
            override fun onNext(it: Transaction) {
                //				it.blockNumber.print()
                GlobalScope.launch {
                    processTransaction(transaction = it)
                }
                Thread.sleep(10)
                etherConfig.saveLastBlock(it.blockNumber)
                lastObservedBlock = it.blockNumber
                lastTransactionAnalyzeTime.set(System.currentTimeMillis())
            }

            override fun onCompleted() {
                log.info("Got last eth block for now: success.")
            }

            override fun onError(p0: Throwable) {
                p0.printStackTrace()
            }

        }

        val web3j = etherOperations.web3j

        web3j.catchUpToLatestTransactionObservable(DefaultBlockParameter.valueOf(lastObservedBlock))
                .filter { it.value != BigInteger.ZERO }
                .subscribe(subscriber)

    }

    suspend fun fastGather() {
        val web3j = etherOperations.web3j

        val list = mutableListOf<Deferred<EthBlock>>()

        val pool = Executors.newFixedThreadPool(100).asCoroutineDispatcher()

        for (i in (lastObservedBlock.toLong())..web3j.ethBlockNumber().send().blockNumber.toLong()) {
            list += GlobalScope.async(pool) { web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(i)), true).send() }
        }

        runBlocking {
            for (future in list) {
                future.await().result.transactions.forEach {
                    (it as Transaction).apply { processTransaction(it) }
                }
            }
        }

    }


}