package br.nom.belo.marcio.simuladorvoo;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static br.nom.belo.marcio.simuladorvoo.Util.interromperThred;
import static br.nom.belo.marcio.simuladorvoo.Util.logarEReinterromper;

class Aviao implements Runnable {

    private Aeroporto aeroporto;
    private String idAviao;
    private long tempoVoo = 0;
    private static final Logger LOG = LoggerFactory.getLogger("Aviao");

    public Aviao(Aeroporto aeroporto, String idAviao, long tempoVoo) {
        this.aeroporto = aeroporto;
        this.idAviao = idAviao;
        this.tempoVoo = tempoVoo;
    }

    public void run() {

        try {

            Thread.sleep(tempoVoo / 2);
        } catch (InterruptedException ie) {

            logarEReinterromper();
        }
        decolar();
        voar();
        aterrisar();
        LOG.info("{} em solo.", idAviao);
        interromperThred();


    }

    private void decolar() {

        LOG.info("{} pedindo autorização ao {} para decolar...", idAviao, aeroporto.getNomeAeroporto());
        aeroporto.esperarPistaDisponivel(idAviao); // Espera uma pista livre
        LOG.info("{} decolando...", idAviao);
    }

    private void voar() {

        LOG.info("{}: voando...", idAviao);
        try {

            Thread.sleep(tempoVoo);
        } catch (InterruptedException e) {

            logarEReinterromper();
        }
    }


    private void aterrisar() {

        LOG.info("{} pedindo autorização ao {} para aterrisar...", idAviao, aeroporto.getNomeAeroporto());
        aeroporto.esperarPistaDisponivel(idAviao); // Espera uma pista livre
        LOG.info("{} aterrisando...", idAviao);
    }


}

class Aeroporto implements Runnable {

    private boolean temPistaDisponivel = true;
    private String nomeAeroporto;
    private Random random = new Random();
    private static final Logger LOG = LoggerFactory.getLogger("Aeroporto");
    private Aviao aviao;

    public Aeroporto(String nomeAeroporto) {
        this.nomeAeroporto = nomeAeroporto;
    }

    public String getNomeAeroporto() {
        return nomeAeroporto;
    }

    public synchronized void esperarPistaDisponivel(String idAviao) {
        while (!temPistaDisponivel) {
            try {
                //Espera por 5s taxiando - Gutemberg
                LOG.info( "{} sem pista disponível. Avião {}, por favor, aguarde...", nomeAeroporto, idAviao);
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        LOG.info("{} autoriza {} para utilizar pista", nomeAeroporto, idAviao);
        temPistaDisponivel = false;

}

    public synchronized void mudarEstadoPistaDisponivel() {

        // Inverte o estado da pista.
        temPistaDisponivel = !temPistaDisponivel;

        LOG.info("{} tem pista disponível? {}", nomeAeroporto, (temPistaDisponivel ? "Sim" : "Não"));

        // Notifica a mudanca de estado para quem estiver esperando.
        if (temPistaDisponivel) this.notifyAll();
    }

    public void run() {

        LOG.info("Rodando aeroporto {}", nomeAeroporto);

        do {
            try {
                mudarEstadoPistaDisponivel();
                // Coloca a thread aeroporto dormindo por um tempo de 0 a 5s
                Thread.sleep(random.nextInt(5000));
            } catch (InterruptedException e) {
                logarEReinterromper();
            }
        } while (true); // NOSONAR
    }
}

/*
 * Simulador de voo com threads
 */
public final class SimuladorVoo {

    private static final Logger LOG = LoggerFactory.getLogger("SimuladorVoo");

    public static void main(String[] args) {

        LOG.info("Rodando simulador de voo.");

        // Constroi aeroporto e inicia sua execucao.
        // NÃO MEXER NESSE TRECHO
        Aeroporto santosDumont = new Aeroporto("Santos Dumont");
        Thread threadAeroporto = new Thread(santosDumont, "santosDumont");

        // Constrói aviao e inicia sua execucao.
        // NÃO MEXER NESSE TRECHO
        Aviao aviao14bis = new Aviao(santosDumont, "Avião 14BIS", 10000);
        Thread thread14bis = new Thread(aviao14bis, "aviao14bis");

        Aviao B52 = new Aviao(santosDumont, "Avião B52", 10000);
        Thread threadB52 = new Thread(B52, "aviaoB52");

        Aviao electra = new Aviao(santosDumont, "Avião Electra", 15000);
        Thread threadElectra = new Thread(electra, "aviaoElectra");

        Aviao legacy600 = new Aviao(santosDumont, "Avião Legacy600", 10000);
        Thread threadLegacy600 = new Thread(legacy600, "aviaoLegacy600");

        Aviao bandeirante = new Aviao(santosDumont, "Avião Bandeirante", 13000);
        Thread threadbandeirante = new Thread(bandeirante, "aviaoBandeirante");

        // Inicia as threads
        threadAeroporto.start();
        thread14bis.start();
        threadB52.start();
        threadElectra.start();
        threadLegacy600.start();
        threadbandeirante.start();


        try {
            // Junta-se ao término da execução da thread do aeroporto
            threadAeroporto.join();
        } catch (InterruptedException ex) {

            logarEReinterromper();
        }
            /*thread14bis.isInterrupted();
            threadAeroporto.isInterrupted();*/
        LOG.info("Terminando thread principal.");
    }
}

class Util {

    private static final Logger LOG = LoggerFactory.getLogger("Util");

    private Util() {
    }

    static void logarEReinterromper() {

        LOG.error("Thread interrompida");
        Thread.currentThread().interrupt();
    }

    static void interromperThred() {
        Thread.interrupted();
    }
}