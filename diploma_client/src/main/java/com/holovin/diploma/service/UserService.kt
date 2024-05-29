package com.holovin.diploma.service

import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class User(val address: String, val nonce: String = UUID.randomUUID().toString())

@Service
class UserService {

  private val users = ConcurrentHashMap<String, User>()

  fun register(address: String) {
    val user = User(address)
    users[address] = user
  }

  fun findByAddress(address: String) = users[address]

}
