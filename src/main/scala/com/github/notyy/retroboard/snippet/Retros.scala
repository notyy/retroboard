package com.github.notyy.retroboard.snippet

import net.liftweb.util.Helpers._
import scala.xml.{Text, NodeSeq}
import com.github.notyy.retroboard.model.{RetroStatus, RetroDB, Retro}
import net.liftweb.http.{S, SHtml}
import com.github.notyy.retroboard.state.{verified, currUser}
import net.liftweb.http.js.JsCmds.{Script, Replace, SetHtml}
import net.liftweb.http.js.jquery.JqJsCmds.{Show, ModalDialog, Hide, FadeIn}
import net.liftweb.common.Loggable
import net.liftweb.http.js.JE.Call
import org.squeryl.PrimitiveTypeMode._


class Retros extends Loggable {

  def list = {
    import RetroStatus._
    val retroStatusMap = Map(Waiting -> "warning", OnGoing -> "error", Complete -> "success")
    val allRetros: List[Retro] = Retro.listAll
    if (allRetros.isEmpty) {
      ".retro-line" #>
        <div class="alert" id="alert">
          <button type="button" class="close" data-dismiss="alert">
            &times;
          </button>
          <strong>注意!</strong>
          现在还没有人创建过回顾
        </div>
    } else {
      ".retro-line" #> allRetros.map {
        retro =>
          logger.info(s"retroInfo\nid=${retro.id}\ntitle=${retro.title}\ncreator=${retro.creator}")
          ".retro-line [class]" #> retroStatusMap(retro.status) &
            ".retro-line [id]" #> s"retro-${retro.id}" &
            ".retro-title *" #> <a href={s"detail?id=${retro.id}"}>{retro.title}</a> &
            ".retro-creator *" #> retro.creator &
            ".retro-status *" #> retro.status.toString
      }
    }
  }

  def detail = {
    val id = S.param("id").getOrElse("0")
    val retro = RetroDB.retros.lookup(id.toLong).get
    if(verified.get.contains(id.toString) && verified.get(id.toString)) {
      ".retro-title" #> retro.title  &
      "retro-creator" #> retro.creator
    } else {
      S.redirectTo(s"verify?retroId=$id")
    }
  }

  def startButton = {
    val id = S.param("id").getOrElse("0")
    "* [onclick]" #> SHtml.ajaxInvoke(startRetro(id.toLong))
  }

  def startRetro(id: Long) = {() =>
    import RetroDB._
    val retro = retros.lookup(id).get
    retro.status = RetroStatus.OnGoing
    retro.update
    S.redirectTo("/")
  }

  def createButton = {
    "* [onclick]" #> SHtml.ajaxInvoke(createNewRetro)
  }

  def createNewRetro = {() =>
    SetHtml("createRetroForm", (new RetroScreen).toForm) &
      FadeIn("createRetroForm", 100 millis, 500 millis) & Hide("alert")
  }
}
