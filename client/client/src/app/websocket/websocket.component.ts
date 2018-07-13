import { Component, OnInit, OnDestroy } from '@angular/core';
import { ZooWebsocketService } from '../zoo-websocket.service';
import { Subscription } from '../../../node_modules/rxjs/Subscription';
import { Guid } from 'guid-typescript';

@Component({
  selector: 'app-websocket',
  templateUrl: './websocket.component.html',
  styleUrls: ['./websocket.component.css']
})
export class WebsocketComponent implements OnInit, OnDestroy {

  public messages: Array<any> = [];
  private MAX_MESSAGES = 100;
  
  private animalSpecies: string;
  private animalName = '';
  private currentTransactionID: string;

  private zoo: any;
  private subscription: Subscription;

  constructor(private zooWebSocketService:ZooWebsocketService) { }

  ngOnInit() {
    this.subscription = 
        this.zooWebSocketService.zooSubject.subscribe(data => this.onWebSocketMessage(data));

    setTimeout(() => this.getZoo(), 250);
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  private onWebSocketMessage(data: any): void {
    const item: any = JSON.parse(data.data);
    item.localTimestamp = Date.now();

    if (  item.event === 'transactionComplete') {
      switch (item.transactionType.value) {
        case 1: //GET_ZOO 
          this.zoo = item.payload;
          break;
      
        case 2: //ADD_ANIMAL
          if (item.triggeringMessage.uuid == this.currentTransactionID) {
            this.reset();
            this.getZoo();
          } 
          break;
      }
    }
    this.messages.unshift(item);
    while (this.messages.length > this.MAX_MESSAGES) {
      this.messages.pop();
    }
  }

  private getZoo(): void {
    const getZooRequest = {
      transactionType: {
        value: 1
      },
      payload: null,
      uuid: Guid.raw()
    };

    console.log(getZooRequest);
    this.zooWebSocketService.zooSubject.next(getZooRequest);
  }

  public addAnimal() {
    const addAnimalRequest = {
      transactionType: {
        value: 2
      },
      payload: {
        name: this.animalName,
        species: this.animalSpecies
      },
      uuid: Guid.raw()
    };
    this.currentTransactionID = addAnimalRequest.uuid;
    this.zooWebSocketService.zooSubject.next(addAnimalRequest);
  }

  private reset(): void {
    this.currentTransactionID = null;
    this.animalName = '';
    this.animalSpecies = null;
  }
}
