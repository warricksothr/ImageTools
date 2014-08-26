package com.sothr.imagetools.dao

import com.sothr.imagetools.util.{PropertiesService, PropertyEnum}
import grizzled.slf4j.Logging
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.hibernate.service.ServiceRegistry

/**
 * Utility class to interface with hibernate
 *
 * Created by drew on 2/8/14.
 */
object HibernateUtil extends Logging {

  private val sessionFactory:SessionFactory = buildSessionFactory()
  private var serviceRegistry:ServiceRegistry = null

  private def buildSessionFactory():SessionFactory = {
    try {
      // Create the SessionFactory from hibernate.cfg.xml
      val configuration = new Configuration().configure("hibernate.cfg.xml")
      //set the database location
      info(s"Connecting to database at: \'${PropertiesService.get(PropertyEnum.DatabaseConnectionURL.toString)}\'")
      configuration.setProperty("hibernate.connection.url", PropertiesService.get(PropertyEnum.DatabaseConnectionURL.toString))
      serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties).build
      configuration.buildSessionFactory(serviceRegistry)
    } catch {
      case ex:Throwable =>
      // Make sure you log the exception, as it might be swallowed
      error("Initial SessionFactory creation failed.", ex)
      throw new ExceptionInInitializerError(ex)
    }
  }

  def getSessionFactory:SessionFactory = {
    sessionFactory
  }


}