// src/app/application/address/address.model.ts

export interface Address {
    id?: number;          // Optionnel si l'ID est généré par le serveur
    street: string;
    postalCode: string;
    city: string;
    country: string;
  }
  