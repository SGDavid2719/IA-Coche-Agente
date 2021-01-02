/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

/**
 *
 * @author David Santome
 */
// Exemple de Cotxo molt bàsic
public class Cotxo24 extends Agent {

    static final boolean DEBUG = false;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;
    static final int COTXE = 1;

    int VELOCITATTOPE = 5;
    int VELOCITATFRE = 4;
    int VELOCITATEMERGENCIA = 3;

    Estat estat;
    int espera = 0;

    // Boolean para saber si previamente hemos colisionado
    boolean colisionPrevia = false;
    // Boolean para saber si al haber colisionado hemos conseguido solventarlo
    boolean intentoFallido = false;

    double desquerra, ddreta, dcentral;

    public Cotxo24(Agents pare) {
        super(pare, "Rayo McQueen", "imatges/RayoMCQueen.png");
    }

    @Override
    public void inicia() {
        setAngleVisors(45);
        setDistanciaVisors(350);
        setVelocitatAngular(9);
    }

    @Override
    public void avaluaComportament() {

        estat = estatCombat();  // Recuperam la informació actualitzada de l'entorn

        // Si volem repetir una determinada acció durant varies interaccions
        // ho hem de gestionar amb una variable (per exemple "espera") que faci
        // l'acció que volem durant el temps que necessitem
        if (espera > 0) {  // no facis res, continua amb el que estaves fent
            espera--;
        } else {

            // ACTUALIZAMOS VISORES
            ddreta = estat.distanciaVisors[DRETA];
            desquerra = estat.distanciaVisors[ESQUERRA];
            dcentral = estat.distanciaVisors[CENTRAL];

            // Si hemos colisionado previamente
            if (colisionPrevia) {
                // Si el intento de seguir circulando 
                if (intentoFallido == false) {
                    dreta();
                    endavant(1);
                    espera = 10;
                    intentoFallido = true;
                } else {
                    intentoFallido = false;
                }
                colisionPrevia = false;
            } else {
                // VEHICULO PARADO O HA PISADO UNA MANCHA
                if ((estaAturat() && estat.enCollisio == false) || (estat.marxa >= 3 && estat.velocitat < 70)) {
                    endavant(1);
                    noGiris();
                } else {
                    // VEHICULO CONTRA DIRECCION
                    if (estat.contraDireccio) {
                        // Miramos distancias y donde haya más sitio giramos
                        if (ddreta > desquerra) {
                            dreta();
                        } else {
                            esquerra();
                        }
                        espera = 10;
                    } else {
                        // VEHICULO COLISIONADO
                        if (estat.enCollisio) {
                            if (estat.objecteVisor[CENTRAL] == COTXE || estat.objecteVisor[DRETA] == COTXE || estat.objecteVisor[ESQUERRA] == COTXE) {
                                // Caso 1: Coche con coche -> Adelantamos o nos separamos de él
                                if (ddreta > desquerra) {
                                    dreta();
                                } else {
                                    esquerra();
                                }
                                endavant(1);
                                espera = 5;
                                return;
                            } else if (dcentral < 20 && ddreta < 50 && desquerra < 50) {
                                // Caso 2: De frente - Perpendicular -> Marcha Atrás
                                enrere(1);
                                noGiris();
                            } else if (ddreta < 20) {
                                // Caso 3: De frente - Lado derecho -> Giramos Izquierda
                                if (dcentral < 110 && estat.objecteVisor[CENTRAL] == 0) {
                                    esquerra();
                                    return;
                                }
                                endavant(1);
                            } else if (desquerra < 20) {
                                // Caso 4: De frente - Lado izquierdo -> Giramos Derecha
                                if (dcentral < 110 && estat.objecteVisor[CENTRAL] == 0) {
                                    dreta();
                                    return;
                                }
                                endavant(1);
                            } else if ((dcentral > 150 && ddreta > 150 && desquerra > 150) || (dcentral > 150 && ddreta > 150 && estat.objecteVisor[ESQUERRA] != 0) || (dcentral > 150 && desquerra > 150 && estat.objecteVisor[DRETA] != 0) || (desquerra > 150 && ddreta > 150 && estat.objecteVisor[CENTRAL] != 0)) {
                                // Caso 5: Esquinas - Giros en U -> Marcha Atrás y Giramos Derecha mediante ColisionPrevia
                                enrere(1);
                                noGiris();
                                colisionPrevia = true;
                            } else if (dcentral < 100 || ddreta < 100 || desquerra < 100) {
                                // Caso 6 y 7: Esquinas - De Lado / Tocamos pared por detrás
                                if (estat.objecteVisor[CENTRAL] == 0) {
                                    endavant(1);
                                } else if (estat.objecteVisor[CENTRAL] == 2 && intentoFallido == false) {
                                    enrere(1);
                                    colisionPrevia = true;
                                } else {
                                    endavant(1);
                                }
                            }
                            noGiris();
                            espera = 15;
                        } else {
                            // Si no estamos en colisión significa que no ha fallado
                            intentoFallido = false;
                            // VEHICULO LINEA RECTA
                            if ((dcentral > 210 && ddreta > 35 && desquerra > 35) || (dcentral > 210 && estat.velocitat < 250)) {
                                // Augmentamos marchas de manera gradual
                                switch (estat.marxa) {
                                    case 1:
                                        endavant(2);
                                        espera = 20;
                                        break;
                                    case 2:
                                        endavant(3);
                                        espera = 20;
                                        break;
                                    case 3:
                                        endavant(4);
                                        espera = 20;
                                        break;
                                    default:
                                        endavant(VELOCITATTOPE);
                                        break;
                                }
                                // VEHICULO DISPARAR Y ADELANTAR
                                // Recorremos el array de enemigos para determinar cual vemos
                                int k;
                                for (k = 0; k < estat.numBitxos; k++) {
                                    if (estat.veigEnemic[k]) {
                                        int sector = estat.sector[k];
                                        if (sector != -1 && estat.marxa > 3) {
                                            Punt puntCE = estat.posicioEnemic[k];
                                            Punt puntC2 = estat.posicio;
                                            double dist4 = puntC2.distancia(puntCE);
                                            if (dist4 < 80) {
                                                // Si está cerca disparamos y recto
                                                if (estat.objecteVisor[CENTRAL] == COTXE) {
                                                    dispara();
                                                }
                                                // Si puede adelantar sin asumir riesgo lo hace
                                                // sino seguirá
                                                if (sector == 3) {
                                                    if (ddreta > 40) {
                                                        dreta();
                                                    }
                                                } else if (sector == 2) {
                                                    if (desquerra > 40) {
                                                        esquerra();
                                                    }
                                                }
                                                endavant(VELOCITATFRE);
                                                return;
                                            }
                                        }
                                    }
                                }
                                // Recorremos el array de objetos para analizar las diferentes posibilidades
                                int i;
                                for (i = 0; i < estat.numObjectes; i++) {
                                    int sector = estat.objectes[i].sector;
                                    if (sector != -1 && estat.marxa > 3) {
                                        switch (estat.objectes[i].tipus) {
                                            // Si es un recurso, está a una distancia próxima, tenemos menos de 3000
                                            // y no supone un riesgo girar para cogerlo intentamos pasar por encima
                                            case RECURSOS:
                                                if (estat.fuel < 3000) {
                                                    Punt puntR = new Punt(estat.objectes[i].posicio.x, estat.objectes[i].posicio.y);
                                                    Punt puntC = estat.posicio;
                                                    double dist = puntC.distancia(puntR);
                                                    if (dist < 20) {
                                                        if (sector == 3) {
                                                            if (desquerra > 40) {
                                                                endavant(VELOCITATFRE);
                                                                esquerra();
                                                                return;
                                                            }
                                                        } else if (sector == 2) {
                                                            if (ddreta > 40) {
                                                                endavant(VELOCITATFRE);
                                                                dreta();
                                                                return;
                                                            }
                                                        }
                                                    }
                                                }
                                                break;
                                            case OLI:
                                                // Si es aceite, está a una distancia próxima, no tenemos ninguno
                                                // y no supone un riesgo girar para cogerlo intentamos pasar por encima
                                                if (estat.oli == 0) {
                                                    Punt puntO = new Punt(estat.objectes[i].posicio.x, estat.objectes[i].posicio.y);
                                                    Punt puntC3 = estat.posicio;
                                                    double dist3 = puntC3.distancia(puntO);
                                                    if (dist3 < 20) {
                                                        if (sector == 3) {
                                                            if (desquerra > 40) {
                                                                endavant(VELOCITATFRE);
                                                                esquerra();
                                                                return;
                                                            }
                                                        } else if (sector == 2) {
                                                            if (ddreta > 40) {
                                                                endavant(VELOCITATFRE);
                                                                dreta();
                                                                return;
                                                            }
                                                        }
                                                    }
                                                }
                                                break;
                                            case TACAOLI:
                                                // Si es una mancha de aceite a una distancia próxima, miramos si es conveniente
                                                // esquivar por el lado opuesto o podemos esquivar por el mismo lado
                                                Punt puntTO = new Punt(estat.objectes[i].posicio.x, estat.objectes[i].posicio.y);
                                                Punt puntC2 = estat.posicio;
                                                double dist2 = puntC2.distancia(puntTO);
                                                if (dist2 < 80) {
                                                    if (sector == 3) {
                                                        if (ddreta > 30) {
                                                            dreta();
                                                            posaOli();
                                                        } else {
                                                            esquerra();
                                                            posaOli();
                                                        }
                                                    } else if (sector == 2) {
                                                        if (desquerra > 30) {
                                                            esquerra();
                                                            posaOli();
                                                        } else {
                                                            dreta();
                                                            posaOli();
                                                        }
                                                    }
                                                    if (estat.velocitat > 400) {
                                                        endavant(VELOCITATFRE);
                                                    } else {
                                                        endavant(VELOCITATTOPE);
                                                    }
                                                    return;
                                                }
                                                break;
                                        }
                                    }
                                }
                                // En caso de no haber situación de interés se sigue recto
                                noGiris();
                            } else {
                                // VEHICULO GIRANDO
                                if (ddreta > desquerra) {
                                    dreta();
                                } else {
                                    esquerra();
                                }
                                // Recorremos el array de objetos para analizar las diferentes posibilidades, en las curvas
                                // no arriesgamos a recoger recursos pero si esquivamos
                                int j;
                                for (j = 0; j < estat.numObjectes; j++) {
                                    int sector = estat.objectes[j].sector;
                                    if (sector != -1 && estat.marxa > 3) {
                                        // Si es una mancha de aceite a una distancia próxima, miramos si es conveniente
                                        // esquivar por el lado opuesto o podemos esquivar por el mismo lado
                                        if (estat.objectes[j].tipus == TACAOLI) {
                                            Punt puntTO = new Punt(estat.objectes[j].posicio.x, estat.objectes[j].posicio.y);
                                            Punt puntC = estat.posicio;
                                            double dist = puntC.distancia(puntTO);
                                            if (dist < 80) {
                                                if (sector == 3) {
                                                    endavant(VELOCITATEMERGENCIA);
                                                    posaOli();
                                                    if (ddreta > 20) {
                                                        dreta();
                                                    } else {
                                                        esquerra();
                                                    }
                                                } else if (sector == 2) {
                                                    endavant(VELOCITATEMERGENCIA);
                                                    posaOli();
                                                    if (desquerra > 20) {
                                                        esquerra();
                                                    } else {
                                                        dreta();
                                                    }
                                                }
                                                return;
                                            }
                                        }
                                    }
                                }
                                // Si vamos excesivamente rápido en las curvas frenamos para no colisionar
                                if (estat.velocitat > 400) {
                                    endavant(VELOCITATFRE);
                                } else {
                                    endavant(VELOCITATTOPE);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
