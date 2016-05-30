package btce.internal

import proto.common.Trade
import rx.Observable

class TradeStreamer {



    fun accept(batch: List<Trade>) {

    }

    fun stream(): Observable<Trade> {
        return Observable.empty()
    }

}