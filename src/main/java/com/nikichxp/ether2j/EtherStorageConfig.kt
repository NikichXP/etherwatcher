package com.nikichxp.ether2j

import com.nikichxp.ether2j.entity.EtherWalletData
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
interface EtherStorageConfig {

    fun getEtherWalletStoreDir(): String {
        return (System.getProperty("user.dir") + "/temp/").replace("//", "/")
    }

    fun addWalletInfo(address: String, file: File, password: String?): EtherWalletData
    fun getWallet(address: String): EtherWalletData
    fun getWalletFile(address: String): File
    fun getWalletList(): List<EtherWalletData>

}