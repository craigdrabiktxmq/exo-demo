import { Component, OnInit, OnDestroy } from '@angular/core';
import { ZooWebsocketService } from '../zoo-websocket.service';
import { Subscription } from '../../../node_modules/rxjs/Subscription';
import { Guid } from 'guid-typescript';
import { MatDialog, MatDialogRef } from '../../../node_modules/@angular/material';
import { WebSocketMessageDialogComponent } from '../web-socket-message-dialog/web-socket-message-dialog.component';
import { Router } from '../../../node_modules/@angular/router';

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

  private getZooTimeout: number;
  private paused = false;

  constructor(private zooWebSocketService: ZooWebsocketService,
              private dialogService: MatDialog,
              private router: Router ) { }

  ngOnInit() {
    this.subscription = 
        this.zooWebSocketService.zooSubject.subscribe(data => this.onWebSocketMessage(data));

    setTimeout(() => this.getZoo(), 250);
  }

  ngOnDestroy() {
    if (this.getZooTimeout) {
      clearTimeout(this.getZooTimeout);
    }
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
    if (this.getZooTimeout) {
      clearTimeout(this.getZooTimeout);
    }

    const getZooRequest = {
      transactionType: {
        value: 1
      },
      payload: null,
      uuid: Guid.raw()
    };

    console.log(getZooRequest);
    this.zooWebSocketService.zooSubject.next(getZooRequest);

    if (!this.paused) {
      this.getZooTimeout = setTimeout(_ => this.getZoo(), 2000);
    }
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

  public viewMessage(message: any) {
    const dialog: MatDialogRef<WebSocketMessageDialogComponent> = this.dialogService.open(
      WebSocketMessageDialogComponent,
      {
        data: {
          message: message
        }
      }
    );
  }

  public toggle() {
    this.paused = !this.paused;
    if (this.paused) {
      if (this.getZooTimeout) {
        clearInterval(this.getZooTimeout);
        this.getZooTimeout = null;
      }
    } else {
      this.getZoo();
    }
  }

  private reset(): void {
    this.currentTransactionID = null;
    this.animalName = '';
    this.animalSpecies = null;
  }

  private goToREST() {
    this.router.navigate(['home']);
  }
}
