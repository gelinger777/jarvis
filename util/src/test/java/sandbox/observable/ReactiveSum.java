package sandbox.observable;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class ReactiveSum { // (1)

  private BehaviorSubject<Double> a = BehaviorSubject.create(0.0);
  private BehaviorSubject<Double> b = BehaviorSubject.create(0.0);
  private BehaviorSubject<Double> c = BehaviorSubject.create(0.0);

  public ReactiveSum() { // (2)
    Observable.combineLatest(a, b, (x, y) -> x + y).subscribe(c);
  }

  public double getA() { // (3)
    return a.getValue();
  }

  public void setA(double a) {
    this.a.onNext(a);
  }

  public double getB() {
    return b.getValue();
  }

  public void setB(double b) {
    this.b.onNext(b);
  }

  public double getC() { // (4)
    return c.getValue();
  }

  public Observable<Double> obsC() {
    return c.asObservable();
  }
}