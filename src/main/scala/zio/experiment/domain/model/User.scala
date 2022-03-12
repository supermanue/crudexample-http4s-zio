package zio.experiment.domain.model

//TODO: use Refined Types here so we restrict the "name" String characteristics (min, max, forbidden chars)
final case class User(id: Int, name: String)
