import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-web-socket-message-dialog',
  templateUrl: './web-socket-message-dialog.component.html',
  styleUrls: ['./web-socket-message-dialog.component.css']
})
export class WebSocketMessageDialogComponent implements OnInit {

  private message: any;

  constructor(@Inject(MAT_DIALOG_DATA) data) {
    this.message = data.message;
  }

  ngOnInit() {
  }

}
