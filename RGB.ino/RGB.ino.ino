#include <SoftwareSerial.h>
#include <Wire.h>

int boton = 2;
int luz1 = 12;
//int luz2 = 13;

int Pin_R =3;
int Pin_G =5;
int Pin_B =6;

int cont_mod[6];

SoftwareSerial BT(7,8);

char String_RGB[3][4];
char codigo[4];
int RGB[3]={0,0,0};
int UIRGB[3]={0,0,0};
int UDRGB[3]={0,0,0};
int Ambos =1;
int datos;
char lado=' ';

void setup(){
  Serial.begin(9600);
  BT.begin(9600);

  pinMode(luz1, OUTPUT);
//  pinMode(luz2, OUTPUT);
  pinMode(boton, INPUT);
   
  pinMode(Pin_R,OUTPUT);
  pinMode(Pin_G,OUTPUT);
  pinMode(Pin_B,OUTPUT);
}

void cambio(boolean accion){
 if(accion){
 Pin_R =3;
Pin_G =5;
Pin_B =6;
  }else{
   Pin_R =9;
Pin_G =10;
Pin_B =11;
    }
  }

void ultimo(boolean accion){
  if(accion){
    analogWrite(Pin_R, UIRGB[0]); 
    analogWrite(Pin_G, UIRGB[1]); 
    analogWrite(Pin_B, UIRGB[2]);
  }else{
    analogWrite(Pin_R, UDRGB[0]); 
    analogWrite(Pin_G, UDRGB[1]); 
    analogWrite(Pin_B, UDRGB[2]);
    }
  }

void enviar(){
    analogWrite(Pin_R, RGB[0]); 
    analogWrite(Pin_G, RGB[1]); 
    analogWrite(Pin_B, RGB[2]);
  }

void apagar(){
    analogWrite(Pin_R, 0); 
    analogWrite(Pin_G, 0); 
    analogWrite(Pin_B, 0);
  }

void loop(){

if(digitalRead(boton)){
digitalWrite(luz1,LOW);
//digitalWrite(luz2,LOW);
      cambio(true);
      ultimo(true);
      cambio(false);
      ultimo(false);
      
      //Serial.println("BTN ON"); 
} else {
  digitalWrite(luz1,HIGH);
  //digitalWrite(luz2,HIGH);
  cambio(true);
     apagar();
      cambio(false);
      apagar();
      //Serial.println("BTN OFF"); 
}
  
if(BT.available() >= 11){


cont_mod[0] = 0;
cont_mod[1] = 0;
cont_mod[2] = 0;
cont_mod[3] = 0;
cont_mod[4] = 0;
cont_mod[5] = 0;

  for(int i = 0; i < 3; i++){
    for(int j = 0; j < 3; j++){    
      String_RGB[i][j] = BT.read();
      if(String_RGB[i][j] == 'I'){
        cont_mod[0]+=1;
      }else if(String_RGB[i][j] == 'D'){
        cont_mod[1]+=1;
      }else if(String_RGB[i][j] == 'A'){
        cont_mod[2]+=1;
      } else if(String_RGB[i][j] == 'U'){
        cont_mod[3]+=1;
      } else if(String_RGB[i][j] == 'E'){
        cont_mod[4]+=1;
      } else{
        cont_mod[5]+=1;
      } 
      
     //Serial.print(String_RGB[i][j]);
      }
    String_RGB[i][4] = '\0'; 
    BT.read();
    //Serial.print("-");
  }

if(cont_mod[0]>cont_mod[1]&&cont_mod[0]>cont_mod[2]&&cont_mod[0]>cont_mod[3]&&cont_mod[0]>cont_mod[4]&&cont_mod[0]>cont_mod[5]){
      cambio(true);
      lado = 'I';
      Ambos = 0;
      //Serial.print("1");
}else if(cont_mod[1]>cont_mod[0]&&cont_mod[1]>cont_mod[2]&&cont_mod[1]>cont_mod[3]&&cont_mod[1]>cont_mod[4]&&cont_mod[1]>cont_mod[5]){
        cambio(false);
        lado = 'D';
        Ambos = 0;
        //Serial.print("2");
}else if(cont_mod[2]>cont_mod[0]&&cont_mod[2]>cont_mod[1]&&cont_mod[2]>cont_mod[3]&&cont_mod[2]>cont_mod[4]&&cont_mod[2]>cont_mod[5]){
        Ambos = 1;
        //Serial.print("3");
      }else if(cont_mod[3]>cont_mod[0]&&cont_mod[3]>cont_mod[1]&&cont_mod[3]>cont_mod[2]&&cont_mod[3]>cont_mod[4]&&cont_mod[3]>cont_mod[5]){
         if(Ambos){
          cambio(true);
      ultimo(true);
      cambio(false);
        ultimo(false);
         }else{
          if(lado == 'I'){
          cambio(true);
      ultimo(true);
      }else{
        cambio(false);
        ultimo(false);
        }
        }
         }else if(cont_mod[4]>cont_mod[0]&&cont_mod[4]>cont_mod[1]&&cont_mod[4]>cont_mod[2]&&cont_mod[4]>cont_mod[3]&&cont_mod[4]>cont_mod[5]){
         if(Ambos){
          cambio(true);
      apagar();
      cambio(false);
        apagar();
         }else{ 
          if(lado == 'I'){
          cambio(true);
      apagar();
      }else{
        cambio(false);
        apagar();
        }
         }
         //Serial.print("4");
      }else{
        //Serial.print("5");
  for(int i = 0; i < 3; i++){
    for(int j = 0; j < 4; j++){
      if(!(String_RGB[i][j] == '0') && !(String_RGB[i][j] == '1') && !(String_RGB[i][j] == '2') && !(String_RGB[i][j] == '3')&&
      !(String_RGB[i][j] == '4') && !(String_RGB[i][j] == '5') && !(String_RGB[i][j] == '6') && !(String_RGB[i][j] == '7') && 
      !(String_RGB[i][j] == '8') && !(String_RGB[i][j] == '9')&& !(String_RGB[i][j] == '\0'))
        {       
         String_RGB[i][j] = '0';
        }
        codigo[j] = String_RGB[i][j];
    }
    sscanf(codigo,"%d",&RGB[i]);
  
      if(Ambos){
        UIRGB[i] = RGB[i];
        UDRGB[i] = RGB[i];
      }else{
      if(lado == 'I'){
        UIRGB[i] = RGB[i];
      }else{
        UDRGB[i] = RGB[i];
        }
      }
  }   

if(Ambos){
      cambio(true);
      enviar();
      cambio(false);
      }
      
    enviar();
    BT.flush();
 } 

}
}

