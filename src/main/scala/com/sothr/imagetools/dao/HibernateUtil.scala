package com.sothr.imagetools.dao

import grizzled.slf4j.Logging
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import com.sothr.imagetools.util.{PropertiesEnum, PropertiesService}

/**
 * Created by drew on 2/8/14.
 */
object HibernateUtil extends Logging {

  private val sessionFactory:SessionFactory = buildSessionFactory()

  private def buildSessionFactory():SessionFactory = {
    try {
      // Create the SessionFactory from hibernate.cfg.xml
      val configuration = new Configuration().configure("hibernate.cfg.xml")
      //set the database location
      info(s"Connecting to database at: \'${PropertiesService.get(PropertiesEnum.DatabaseConnectionURL.toString)}\'")
      configuration.setProperty("hibernate.connection.url", PropertiesService.get(PropertiesEnum.DatabaseConnectionURL.toString))
      return configuration.buildSessionFactory
    } catch {
      case ex:Throwable =>
      // Make sure you log the exception, as it might be swallowed
      error("Initial SessionFactory creation failed.", ex)
      throw new ExceptionInInitializerError(ex)
    }
  }

  def getSessionFactory():SessionFactory = {
    sessionFactory
  }


}
