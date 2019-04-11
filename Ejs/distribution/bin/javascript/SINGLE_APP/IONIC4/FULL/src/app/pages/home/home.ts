import { ElementRef, Component, ViewChild } from '@angular/core';
import { NavController } from '@ionic/angular';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';

declare var app_toc: any;

@Component({
  selector: 'app-home',
  templateUrl: 'home.html',
  styleUrls: ['home.scss']
})
export class HomePage {
  currentPage: any;
  pages: any;
  url: SafeUrl;
  id: string;
  
  constructor(private route: ActivatedRoute, public navCtrl: NavController, private sanitizer: DomSanitizer, public myElement: ElementRef) {
	// app vars
	this.pages = app_toc;
	this.id = this.route.snapshot.paramMap.get('id');
	
	// navigation vars
    this.currentPage = this.pages[+this.id];
	this.url = sanitizer.bypassSecurityTrustResourceUrl(this.currentPage.url);	
  }	
  
}
