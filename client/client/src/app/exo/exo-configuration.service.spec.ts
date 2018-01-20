import { TestBed, inject } from '@angular/core/testing';

import { ExoConfigurationService } from './exo-configuration.service';

describe('ExoConfigurationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ExoConfigurationService]
    });
  });

  it('should be created', inject([ExoConfigurationService], (service: ExoConfigurationService) => {
    expect(service).toBeTruthy();
  }));
});
