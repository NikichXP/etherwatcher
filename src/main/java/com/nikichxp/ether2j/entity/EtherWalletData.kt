package com.nikichxp.ether2j.entity

abstract class EtherWalletData(var id: String) {

	var address: String
		get() = id
		set(value) {
			id = value
		}

	abstract var fileData: ByteArray

	var password: String? = null
	
}