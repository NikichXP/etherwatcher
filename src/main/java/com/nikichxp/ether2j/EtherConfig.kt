package com.nikichxp.ether2j

import okhttp3.OkHttpClient
import org.springframework.context.annotation.Configuration
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Transaction
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import java.util.concurrent.TimeUnit

@Configuration
interface EtherConfig {

    fun saveLastBlock(blockNumber: BigInteger)
    fun getLastBlock(): BigInteger

    fun onEachTransaction(transaction: Transaction)

    /**
     * Recommended implementation:
     *
    val builder = OkHttpClient.Builder()
    builder.connectTimeout(3, TimeUnit.MINUTES)
    builder.readTimeout(3, TimeUnit.MINUTES)
    builder.writeTimeout(3, TimeUnit.MINUTES)
    return Web3j.build(HttpService("https://mainnet.infura.io/v3/795c2acf200a4cf4a18e4a3a2a3db0ad", builder.build(), true))
     *
     */
    fun buildWeb3j(): Web3j

}
