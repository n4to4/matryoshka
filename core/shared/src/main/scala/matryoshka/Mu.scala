/*
 * Copyright 2014–2016 SlamData Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package matryoshka

import Recursive.ops._

import scalaz._, Scalaz._

/** This is for inductive (finite) recursive structures, models the concept of
  * “data”, aka, the “least fixed point”.
  */
final case class Mu[F[_]](unMu: λ[A => (F[A] => A)] ~> Id)
object Mu {
  implicit val recursive: Recursive[Mu] = new Recursive[Mu] {
    def project[F[_]: Functor](t: Mu[F]) = lambek(t)
    override def cata[F[_]: Functor, A](t: Mu[F])(f: F[A] => A) = t.unMu(f)
  }

  implicit val corecursive: Corecursive[Mu] = new Corecursive[Mu] {
    def embed[F[_]: Functor](t: F[Mu[F]]) =
      Mu(new (λ[A => (F[A] => A)] ~> Id) {
        def apply[A](fa: F[A] => A): A = fa(t.map(_.cata(fa)))
      })
  }

  implicit val equalT: EqualT[Mu] = Recursive.equalT[Mu]

  implicit val showT: ShowT[Mu] = Recursive.showT[Mu]
}
