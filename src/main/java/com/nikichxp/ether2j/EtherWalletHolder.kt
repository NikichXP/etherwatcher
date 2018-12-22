package com.nikichxp.ether2j

import com.nikichxp.ether2j.entity.EtherWalletData
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.io.File
@Service
class EtherWalletHolder(
		private val etherConfig: EtherConfig,
		private val etherStorageConfig: EtherStorageConfig,
		@Lazy val etherOperations: EtherOperations) {

	private val keyStore = etherStorageConfig.getEtherWalletStoreDir()

	init {
		val dir = File(keyStore)
		if (!dir.exists()) {
			dir.mkdir()
		}
	}

	fun listWalletAddresses(): List<String> {
		return etherStorageConfig.getWalletList().map { it.address }
	}

	fun listWalletBalances(): Map<String, Double> {
		return mapOf(*listWalletAddresses().map { it to etherOperations.getBalance(it) }.toTypedArray())
	}

	fun addWallet(address: String, data: ByteArray, pass: String?): EtherWalletData {
		val file = File(keyStore + address)
		file.createNewFile()
		file.writeBytes(data)
		return addWallet(address, file, pass)
	}

	fun addWallet(address: String, file: File, pass: String?): EtherWalletData {
		return etherStorageConfig.addWalletInfo(address, file, pass)
	}

	fun getWallet(address: String): EtherWalletData {
		return etherStorageConfig.getWallet(address)
	}

	fun getWalletFile(address: String): File {
		return etherStorageConfig.getWalletFile(address)
	}
}