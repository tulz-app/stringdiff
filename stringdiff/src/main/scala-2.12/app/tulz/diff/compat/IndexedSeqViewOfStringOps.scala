package app.tulz.diff.compat

class IndexedSeqViewOfStringOps(
  underlying: IndexedSeqView[String]
) {

  def mkString: String = underlying.underlying.mkString

}
