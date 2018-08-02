import { Component, OnInit } from '@angular/core';
import { DefaultService } from '../../api/index';
import { Zoo } from '../../api/model/zoo';
import { useAnimation } from '@angular/core/src/animation/dsl';
import { ExoPlatformService } from '../exo/exo-platform.service';
import { Router } from '../../../node_modules/@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  public animalName:String = '';
  public animalSpecies:String;

  public zoo:Zoo;
  
  public get useHashgraph():string {
    return this.service.useHashgraph.toString();
  }

  public set useHashgraph(useHashgraph:string) {
    this.service.useHashgraph = useHashgraph === 'true';
  }

  constructor(private service:DefaultService, 
              private router: Router,
              platform:ExoPlatformService) {
    platform.platformReady.subscribe(_ => {
      this.refreshZoo();
      setInterval(() => this.refreshZoo(), 2000);
    });
  }

  private addAnimal():void {
    let animal:any = {name: this.animalName, species: this.animalSpecies};
    this.service.addAnimal(animal).subscribe(_ => {
      this.animalName = '';
      this.animalSpecies = undefined;
      this.refreshZoo();
    });
  }

  private refreshZoo() {
    this.service.getZoo().subscribe(result => this.zoo = result.payload );
  }

  private sendShutdown() {
    this.service.stopHashgraph().subscribe(result => alert("Hashgraph shut down!"));
  }
  private goToWebsocket() {
    this.router.navigate(['websocket']);
  }
  ngOnInit() {
  }

}
