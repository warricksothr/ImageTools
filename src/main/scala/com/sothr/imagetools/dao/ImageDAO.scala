package com.sothr.imagetools.dao

import org.hibernate.{Session, SessionFactory}
import com.sothr.imagetools.image.Image

/**
 * Created by drew on 2/8/14.
 */
class ImageDAO {

  private val sessionFactory:SessionFactory = HibernateUtil.getSessionFactory()
  private val example:Image = new Image()

  def find(path:String):Image = {
    val session:Session = sessionFactory.getCurrentSession
    session.beginTransaction
    val result = session.get(example.getClass, path).asInstanceOf[Image]
    session.getTransaction.commit()
    result
  }

  def save(image:Image) = {
    val session:Session = sessionFactory.getCurrentSession
    session.beginTransaction

    session.saveOrUpdate(image)

    session.getTransaction.commit()
  }

  def save(images:List[Image]) = {
    val session:Session = sessionFactory.getCurrentSession
    session.beginTransaction

    for (image <- images) session.saveOrUpdate(image)

    session.getTransaction.commit()
  }

}
