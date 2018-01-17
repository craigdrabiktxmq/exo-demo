import { TestBed, inject } from '@angular/core/testing';

import { ExoDistributedEndpointService } from './exo-distributed-endpoint.service';

describe('ExoDistributedEndpointService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ExoDistributedEndpointService]
    });
  });

  it('should be created', inject([ExoDistributedEndpointService], (service: ExoDistributedEndpointService) => {
    expect(service).toBeTruthy();
  }));
});
