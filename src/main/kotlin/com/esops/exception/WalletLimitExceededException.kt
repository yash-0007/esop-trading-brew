package com.esops.exception

class WalletLimitExceededException(val errorList: List<String>) : Throwable() {}
