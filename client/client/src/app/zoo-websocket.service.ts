import { Injectable } from '@angular/core';
import { Subject } from '../../node_modules/rxjs/Subject';
import { Observable } from '../../node_modules/rxjs/Observable';
import { Observer } from '../../node_modules/rxjs/Observer';

@Injectable()
export class ZooWebsocketService {

  private websocket: WebSocket;
  public zooSubject: Subject<any>;

  constructor() { 
    this.websocket = new WebSocket('ws://localhost:53204/wstest');
    const observable: Observable<any> = Observable.create((obs: Observer<any>) => {
      this.websocket.onmessage = obs.next.bind(obs);
      this.websocket.onerror = obs.error.bind(obs);
      this.websocket.onclose = obs.complete.bind(obs);
      return this.websocket.close.bind(this.websocket);
    });

    const observer: any = {
      next: (data: any) => {
        if (this.websocket.readyState === WebSocket.OPEN) {
          this.websocket.send(JSON.stringify(data));
        }
      }
    };

    this.zooSubject = Subject.create(observer, observable);
  }
}
