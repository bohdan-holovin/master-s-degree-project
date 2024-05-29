package com.holovin.diploma.web3auth

import org.springframework.security.authentication.AbstractAuthenticationToken

class Web3Authentication(private val address: String, private val signature: String) : AbstractAuthenticationToken(null) {

    override fun getPrincipal() = address
    override fun getCredentials() = signature

}
