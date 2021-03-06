/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MDB;

import entities.*;
import static java.lang.Integer.parseInt;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author tibha
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = "jms/myTopic")
    ,
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/myTopic")
    ,
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    ,
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "jms/myTopic")
    ,
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
    ,
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "toMDB = true")
})
public class MDBLog implements MessageListener {
    
    public MDBLog() {
    }
    
    @Override
    public void onMessage(Message message) {
        try {
            TextMessage tm = (TextMessage) message;
            Calendar myCalendar = Calendar.getInstance();
            Date myDate = myCalendar.getTime();
            int id = parseInt(tm.getText());
            
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("EJBModulePU");
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            Demande c = new Demande();
            try
            {
                c= em.find(Demande.class, id);                        
                em.getTransaction().commit();
            }
            catch(Exception e)
            {
                em.getTransaction().rollback();
            }
            Date dateDemande = c.getDateHeureDemande();
            int secondes = MinutesBetween(dateDemande,myDate);
            int heures = secondes / (60 * 60);secondes = secondes % (60 * 60);
            int minutes = secondes / (60);secondes = secondes % (60);
            //ajout dans log
            Logs log = new Logs();
            log.setIdLogs(id);
            log.setInfos("La durée de traitement de l’analyse " + tm.getText() + " a été de " + heures +  " h " + minutes + " min " + secondes + " secondes");
            try
            {
                em.getTransaction().begin();
                em.persist(log);
                em.getTransaction().commit();
            }
            catch(Exception e)
            {
                em.getTransaction().rollback();
            }
            finally
            {
                em.close();
            }
        } catch (JMSException ex) {
            Logger.getLogger(MDBLog.class.getName()).log(Level.SEVERE, null, ex);
        }      
    } 
    public int MinutesBetween(Date d1, Date d2){
             return (int)( (d2.getTime() - d1.getTime()) / (1000));
     } 
    
}
