package com.esops.exception

class UserNotUniqueException(val errorList: List<String>) : Throwable() {}
