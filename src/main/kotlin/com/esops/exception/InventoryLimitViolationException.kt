package com.esops.exception

class InventoryLimitViolationException(val errorList: List<String>) : Throwable() {}
