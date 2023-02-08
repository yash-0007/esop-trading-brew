package com.esops.exception

class UserNotFoundException(val errorList: List<String>) : Throwable() {}
