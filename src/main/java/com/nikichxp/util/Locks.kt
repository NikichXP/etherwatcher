package com.nikichxp.util

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

object Locks {
	
	private val locks = ConcurrentHashMap<String, Mutex>()
	
	suspend fun withLock(lockId: String, function: () -> Unit) {
		locks.getOrPut(lockId, { Mutex() })
			.withLock {
				function.invoke()
			}
	}
	
	fun withBlock(lockId: String, function: () -> Unit) {
		runBlocking { withLock(lockId, function) }
	}
}