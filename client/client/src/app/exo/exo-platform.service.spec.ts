import { TestBed, inject } from '@angular/core/testing';

import { ExoPlatformService } from './exo-platform.service';

describe('ExoPlatformService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ExoPlatformService]
    });
  });

  it('should be created', inject([ExoPlatformService], (service: ExoPlatformService) => {
    expect(service).toBeTruthy();
  }));
});
