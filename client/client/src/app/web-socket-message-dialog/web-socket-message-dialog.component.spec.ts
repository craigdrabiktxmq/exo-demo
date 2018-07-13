import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WebSocketMessageDialogComponent } from './web-socket-message-dialog.component';

describe('WebSocketMessageDialogComponent', () => {
  let component: WebSocketMessageDialogComponent;
  let fixture: ComponentFixture<WebSocketMessageDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WebSocketMessageDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WebSocketMessageDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
