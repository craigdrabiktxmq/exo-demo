import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HeomeComponent } from './heome.component';

describe('HeomeComponent', () => {
  let component: HeomeComponent;
  let fixture: ComponentFixture<HeomeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HeomeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HeomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
