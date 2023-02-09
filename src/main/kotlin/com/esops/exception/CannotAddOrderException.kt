package com.esops.exception

class CannotAddOrderException(val errorList: List<String>) : Throwable()
