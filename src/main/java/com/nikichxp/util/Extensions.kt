package com.nikichxp.util

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.web3j.utils.Convert
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

fun Any.print(text: String = "", prefix: String = "") {
	println(prefix + this.toString() + text)
}

fun BigInteger.toEthFromWei(): Double = Convert.fromWei(this.toBigDecimal(), Convert.Unit.ETHER).toDouble()

fun <T> Deferred<T>.blockAwait(): T = runBlocking { this@blockAwait.await() }

fun String.join(middle: String = "", vararg others: String): String {
	val ret = StringBuilder(this)
	others.forEach {
		ret.append(middle).append(it)
	}
	return ret.toString()
}

fun Any.toMap(): MutableMap<String, Any?> = ObjectToMapConverter.convert(this)

fun Any.print(prefix: String = "") {
	println(prefix + this.toString())
}

fun Boolean?.nullIfFalse() = if (this == true) true else null

fun <T> T.isAnyOf(vararg objs: T): Boolean {
	return objs.any { this == it }
}

fun Random.int(range: IntRange): Int {
	if (range.first == range.last) return range.first
	return this.nextInt(range.last - range.start) + range.start
}

fun <E> Collection<E>.random(): E {
	return this.elementAt(Random().int(0..this.size))
}

fun <E> Collection<E>.randomOrNull(): E? {
	return if (this.isNotEmpty()) this.elementAt(Random().int(0..this.size)) else null
}

inline fun <S, T: S> Iterable<T>.reduceOrElse(defaultValue: S, operation: (acc: S, T) -> S): S {
	val iterator = this.iterator()
	if (!iterator.hasNext()) return defaultValue
	var accumulator: S = iterator.next()
	while (iterator.hasNext()) {
		accumulator = operation(accumulator, iterator.next())
	}
	return accumulator
}

fun LocalDateTime.toLong(): Long = this.toInstant(ZoneOffset.ofHours(2)).toEpochMilli()

fun Long.toLocalDateTime(): LocalDateTime = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

fun String.replace(vararg pairs: Pair<String, String>): String {
	var string = this
	pairs.forEach {
		string = string.replace(it.first, it.second)
	}
	return string
}

