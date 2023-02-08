package com.esops.exception

class InventoryLimitExceededException(val errorList: List<String>) : Throwable() {}
