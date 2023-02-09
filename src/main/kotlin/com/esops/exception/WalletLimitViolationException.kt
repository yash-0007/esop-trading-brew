package com.esops.exception

class WalletLimitViolationException(val errorList: List<String>) : Throwable() {}
