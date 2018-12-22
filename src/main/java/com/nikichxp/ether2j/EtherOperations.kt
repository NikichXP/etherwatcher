package com.nikichxp.ether2j

import com.nikichxp.util.toEthFromWei
import org.springframework.stereotype.Service
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.response.Transaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Transfer
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.util.logging.Logger

/**
 * Working as an API for all our actions in one place
 */
@Service
class EtherOperations(
		private val etherConfig: EtherConfig,
		private val etherWalletHolder: EtherWalletHolder) {

	private val network = "mainnet"
	final var web3j: Web3j = etherConfig.buildWeb3j()
	private val log = Logger.getLogger("ether-operations")

	init {
		log.info("Connected to Ethereum client version: " + web3j.web3ClientVersion().send().web3ClientVersion)
		log.info("Last block ${web3j.ethBlockNumber().send().blockNumber}")
	}

	fun makeTransaction(from: String, to: String, amount: Double, password: String? = null): TransactionReceipt {

		val wallet = etherWalletHolder.getWallet(from)

		val credentials = WalletUtils.loadCredentials(
				password ?: wallet.password,
				etherWalletHolder.getWalletFile(from))
		log.info("Credentials loaded")


		log.info("Sending 1 Wei (${Convert.fromWei("1", Convert.Unit.ETHER).toPlainString()} Ether)")
		val transferReceipt = Transfer.sendFunds(web3j, credentials, to, BigDecimal.valueOf(amount), Convert.Unit.ETHER)
			.send()
		log.info("Transaction complete, view it at https://etherscan.io/tx/" + transferReceipt.transactionHash)
		return transferReceipt
	}

	fun getBalance(address: String): Double =
		web3j.ethGetBalance(address, DefaultBlockParameter.valueOf(
				etherConfig.getLastBlock()))
			.send()
			.balance
			.toEthFromWei()

    /**
     * Coming soon! (probably in release version)
     */
	fun getTransactionInfo(transaction: String): Transaction {
		TODO()
	}
}