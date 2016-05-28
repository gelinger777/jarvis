package common

import proto.common.Order

data class OrderBatch(val time: Long, val orders: List<Order>)