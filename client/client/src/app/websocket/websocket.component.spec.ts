import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WebsocketComponent } from './websocket.component';

describe('WebsocketComponent', () => {
  let component: WebsocketComponent;
  let fixture: ComponentFixture<WebsocketComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WebsocketComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WebsocketComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
