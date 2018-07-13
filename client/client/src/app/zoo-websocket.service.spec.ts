import { TestBed, inject } from '@angular/core/testing';

import { ZooWebsocketService } from './zoo-websocket.service';

describe('ZooWebsocketService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ZooWebsocketService]
    });
  });

  it('should be created', inject([ZooWebsocketService], (service: ZooWebsocketService) => {
    expect(service).toBeTruthy();
  }));
});
