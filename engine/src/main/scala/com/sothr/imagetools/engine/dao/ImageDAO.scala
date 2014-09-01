package com.sothr.imagetools.engine.dao

import com.sothr.imagetools.engine.image.Image
import org.hibernate.{Session, SessionFactory}

/**
 * Interact with stored images
 *
 * Created by drew on 2/8/14.
 */
class ImageDAO {

  private val sessionFactory: SessionFactory = HibernateUtil.getSessionFactory

  def find(path: String): Image = {
    val session: Session = sessionFactory.getCurrentSession
    session.getTransaction.begin()
    val result = session.get(classOf[Image], path).asInstanceOf[Image]
    session.getTransaction.commit()
    result
  }

  def save(image: Image) = {
    val session: Session = sessionFactory.getCurrentSession
    session.getTransaction.begin()
    session.saveOrUpdate(image)
    session.getTransaction.commit()
  }

  def save(images: List[Image]) = {
    val session: Session = sessionFactory.getCurrentSession
    session.getTransaction.begin()
    for (image <- images) session.saveOrUpdate(image)
    session.getTransaction.commit()
  }

  def delete(image: Image) = {
    val session: Session = sessionFactory.getCurrentSession
    session.getTransaction.begin()
    session.delete(image)
    session.getTransaction.commit()
  }

}
