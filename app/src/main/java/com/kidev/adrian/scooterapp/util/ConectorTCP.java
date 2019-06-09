package com.kidev.adrian.scooterapp.util;


import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author agarcia.gonzalez
 */
public class ConectorTCP {

    private int paqueteId;
    private String nick;
    private String token;

    private boolean realizandoConexion;
    private List<PaqueteServidor> peticiones;

    private boolean conectado;
    private boolean conectando;

    private Socket echoSocket;
    private PrintWriter out;
    private BufferedReader in;

    // Singleton
    private static ConectorTCP instance;

    private final long TIMEOUT = 30000;
    private final String hostServerName="192.168.43.229";//"192.168.1.132";
    private final int port = 4444;

    // Test values
    private String outMessage;
    private String inMessage;

    private ConectorTCP() {
        paqueteId=10;
        peticiones = new ArrayList<>();
        conectado=false;
        iniciar();
    }

    public static ConectorTCP getInstance () {
        if (instance==null) {
            iniciarServidor ();
        }

        return instance;
    }

    private boolean iniciar() {
        new InicializarConexion().execute();
        return conectado;
    }

    public static boolean iniciarServidor () {
        instance=new ConectorTCP();
        return true;
    }

    public void realizarConexion (String uri, Map<String,String> parametros, CallbackRespuesta response) {
        realizarConexion(nick,token,uri,getPaqueteID(),parametros,response);
    }

    // Para tests
    public void realizarConexion (String nick, String token, String uri, String paqueteid, Map<String,String> parametros, CallbackRespuesta response) {
        // Si no existen los parametros, se crea
        if (parametros==null)
            parametros=new HashMap<>();

        if (!conectado) {
            if (!iniciar ()) {
                //RuntimeException e = new RuntimeException ();
                parametros.put("error", "No se ha podido realizar la conexión");
                response.error(parametros, Util.CODIGO.notConnection);
                return;
            }
        }

        // Ponemos los valores para realizar la conexión
        PaqueteServidor paquete = new PaqueteServidor();
        paquete.setIdPaquete(paqueteid);
        paquete.setNick(nick);
        paquete.setToken(token);
        paquete.setArgumentos(parametros);
        paquete.setUri(uri);
        paquete.setCallback(response);

        if (realizandoConexion) {
            peticiones.add(paquete);
        } else {
            //realizar conexión
            new RealizarConexion().execute(paquete);
        }
    }

    public synchronized void nextQuery () {
        if (peticiones.size()>0) {
            new RealizarConexion().execute (peticiones.remove(0));
        } else {
            realizandoConexion=false;
        }
    }


    private String getPaqueteID () {
        if (paqueteId==100)
            paqueteId=10;
        return Integer.toString(paqueteId++);
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOutMessage() {
        return outMessage;
    }

    public String getInMessage() {
        return inMessage;
    }



    /**
     ==========================================================

     *  CLASES PARA REALIZAR LA CONEXION ASINCRONAMENTE

     * ==========================================================
     */

    private class InicializarConexion extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (conectando) {
                Log.e("doInBackground", "Ya está conectandose");
                return null;
            }
            conectando=true;
            try {
                echoSocket = new Socket(hostServerName, port);
                out = new PrintWriter(echoSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                conectado=true;
                Log.e("Conexion exitosa", "Se ha conectado a la base de datos");
            } catch (IOException ex) {
                Log.e("Conexión erronea", "ERROR: " + ex.getMessage());
                conectado=false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void paquete) {

        }
    }

    private class RealizarConexion extends AsyncTask<PaqueteServidor, Void, PaqueteCliente> {

        private PaqueteServidor paqueteServidor;

        @Override
        protected PaqueteCliente doInBackground(PaqueteServidor... paquetes) {
            if (paquetes.length!=1) {
                throw new RuntimeException("No se ha podido realizar la conexión porque faltan paquetes");
            }

            paqueteServidor = paquetes[0];

            try  {
                // Como primer valor le enviamos el nombre y nº de jugadores al servidor
                String request = Util.packFromServer(paqueteServidor);

                outMessage = request;

                // Le envio la info al servidor
                out.println(request);

                try {
                    // Leo la respuesta del servidor
                    String respuesta = in.readLine();

                    inMessage = respuesta;

                    // Muestro la respuesta sin procesar (Solo para debug)
                    //System.out.println("Respuesta: "+respuesta);

                    PaqueteCliente paqueteCliente = Util.unpackToCliente(respuesta);

                    return paqueteCliente;
                } catch (SocketException err) {
                    System.err.println("Error en el envio de datos. " + err.toString());
                }

            } catch (UnknownHostException e) {
                System.err.println("No se conoce el host: " + hostServerName);
            } catch (IOException e) {
                System.err.println("No hay conexión para " + hostServerName);
            }

            return null;
        }

        @Override
        protected void onPostExecute(PaqueteCliente paqueteCliente) {

            if (paqueteCliente!=null) {
                Util.CODIGO codigo = paqueteCliente.getCodigo();
                if (codigo.getCodigo()>=200 && codigo.getCodigo()<=299) {
                    paqueteServidor.getCallback().success(paqueteCliente.getArgumentos());
                } else {
                    paqueteServidor.getCallback().error(paqueteCliente.getArgumentos(), paqueteCliente.getCodigo());
                }
            } else {
                paqueteServidor.getCallback().error(paqueteCliente.getArgumentos(), Util.CODIGO.timeOut);
            }

            // Siguiente Query
            nextQuery();
        }
    }
}
