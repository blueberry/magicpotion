Let's taste the basic flavors of Magic Potion.
You may just read the text and follow the code, or you may [install](Quickstart.md)
Magic Potion and follow the tutorial while typing the code in your editor.
[The code used in this tutorial](http://code.google.com/p/magicpotion/source/browse/#hg/src/test/clojure/org/uncomplicate/magicpotion/examples/parties)
is a part of Magic Potion test suite and is available
[here](http://code.google.com/p/magicpotion/source/browse/#hg/src/test/clojure/org/uncomplicate/magicpotion/examples/parties).



# Example 1: Getting Started #
We'll start with a sort of a helloworld-ish example. Here's how a very simple domain
model might look like. [See the source including tests](http://code.google.com/p/magicpotion/source/browse/src/test/clojure/org/uncomplicate/magicpotion/examples/parties/example1.clj).

![http://wiki.magicpotion.googlecode.com/hg/figures/mp-example-01-concept.png](http://wiki.magicpotion.googlecode.com/hg/figures/mp-example-01-concept.png)

Magic Potion is going to help us create a Domain Specific Language (DSL)
that have `person` and `fitst-name` in its vocabulary.

## Importing Magic Potion ##

As with any other Java library, we first have to import the stuff we use.
In Clojure's terminology, we'll `use` (or `require`) `magicpotion` namespace.
```
(use 'org.uncomplicate.magicpotion)
(use 'org.uncomplicate.magicpotion.predicates)
```
Namespace `org.uncomplicate.magicpotion` contains the core Magic Potion
API, while `org.uncomplicate.magicpotion.predicates` offers some helpful predicates
for bulding constraints and is not mandatory.

## Concepts and properties: describing general things ##

If you have ever worked with the relational database or programmed in an
object-oriented language, you are already used to a practice of defining the
schema, or "vocabulary" of your data. You describe the types that are used to
control possible shapes of the data you are working with.
These constructs are then used by the environment to check
the actual data that you pour into your application.
In Magic Potion, this general descriptiprion consists of
_concepts_, _properties_, _roles_, and _restriction functions_
and is a bit different than database schemas or OO types,
but for now, you do not need to concern yourself with that.

It is enough to understand that:
  * **concept** is used to define the general entities of your business domain, like car, employee, product, purchase etc.;
  * **property** defines general characteristics that can be "attached" to concepts, like name, works-at, color, sold-to etc.;
  * **role** is the actual "attachment" of a particular property to a particular concept, like _employee works-at_;
  * **restriction function** is a Clojure function that receives a value of a statement and check this value for validity.

![http://wiki.magicpotion.googlecode.com/hg/figures/mp-example-01-concept.png](http://wiki.magicpotion.googlecode.com/hg/figures/mp-example-01-concept.png)

First, we define `first-name`, by calling a `property` macro and giving
it the sequence (vector, list etc.) of _restriction functions_ that should be
applied to any statement that is defined by that property. We are defining
the property's _range_. Restriction function is a predicate,
which can be any Clojure function that receives an argument.

```
(property first-name
  [string?
   (length-between 2 32)])
```

Properties are first-class citizens in Magic Potion. Unlike UML attributes
or Java fields, they are independent of the concepts, so one property
may be attached to many concepts.

When we create a concept, by calling `concept` macro, we are giving it
the sequence of all properties that are "attached" to it. This concept is in
these properties' _domain_.

```
(concept person
  [first-name])
```

... and that would define our tiny hello world business domain.
Now, we want to use it.

## Individuals: stating the facts about concrete things ##

For each concept you create, Magic Potion creates a Clojure function that
you can use to create concrete individuals that represent concrete things.
For example, calling `person` function with some data...
```
(person ::first-name "Jenna")
```
... creates a Clojure map that contains statement about some particular person.
In this case, this is a statement that says that there is a person whose first
name is Jenna.
```
{::first-name "Jenna"}
```

Now, the obvious question would be: "Why don't we just directly create that
map? At the end, it is an ordinary Clojure map."
Try this:
```
(person :A-keyword-is-not-a-persons-name)
```
You'll get an `IllegalArgumentException`.

**Magic Potion _validates the data you
give to it, and will refuse to create statements that are not in accordance
with the model you have described_. Even more, it will give you a detailed
restriction violation report**.

The individuals are represented by plain
Clojure maps. You get the benefit of being able to use them as any other
Clojure map. On top of that, Magic Potion adds metadata to these maps, so
it can help you with data validation or in any case where it might be benefitial
to know more about your data, like in multimethod dispatching.

## Storing and accessing individuals ##
Since Magic Potion works with plain Clojure maps, you can do
whatever you would do without Magic Potion - there are no special
requirements regarding that. Here are a few guidelines about what makes
most sense to do.

The maps that our new DSL contains _immutable values_ of statements that describe
an individual at certain point in time. They can never ever change again,
in the same way that the name "Jenna" is always the name "Jenna",
even if a girl changes the name to "Mary". What can change, though, is to what
value the _identity_ of the individual references.

For example, we can bind the statement map to a clojure var (not so common, but
useful as an example).
```
(def var-jenna (person ::first-name "Jenna"))
```

That var now represents an individual and holds the current set of statements.
Evaluating `var-jenna` gives us `{::first-name "Jenna"`}.

The recommended way of representing the _identity_ of individuals is with _refs_,
or _atoms_. Refs represent the actual individual (a girl currently called Jenna)
at all points in time. The actual value, `{::first-name "Jenna"`}, that it holds
is the current data about the individual.
```
(def ref-jenna (ref (person ::first-name "Jenna")))
```

When we evaluate the var, we get the ref representing the individual:
```
ref-jenna
```
yields
```
#<Ref@5c95da38: {::first-name "Jenna"}>
```
and if we dereference it (with Clojure `deref` function or
reader macro `@`, we get the actual statements that are currently accurate.
```
@ref-jenna
```
yields
```
{::first-name "Jenna"}
```

Of course, you can store the ref or the value itself in another map,
in a sequence, key-value datastore or in a relational database
(you'll need to choose and possibly implement
the serialization/deserialization strategy).

The whole [immutable values](ImmutableValues.md)/
[mutable references](MutableReferences.md) is one of central themes in Clojure
(and Magic Potion) but we won't discuss it much here since it is an advanced topic.
Just be sure to read about it when you learn the basic stuff.

# Example 2: Inheritance #

![http://wiki.magicpotion.googlecode.com/hg/figures/mp-example-02-inheritance.png](http://wiki.magicpotion.googlecode.com/hg/figures/mp-example-02-inheritance.png)

```
(property aname
          [string?])

(concept party
         [aname])
```

```
(property first-name
          [(length-between 2 32)]
          [aname])

(property last-name
          [(min-length 3) (max-length 32)]
          [aname])
```

```
(concept person
         [first-name
          last-name]
         [party])

(concept company
         [(val> aname [(length-between 2 64)])]
         [party])
```

```
(person)
```

```
{::aname nil ::first-name nil ::last-name nil}
```

```
(person ::first-name "A")
```

```
(thrown? IllegalArgumentException
           (company ::aname "C"))
```

# Where are the methods/procedures/etc. ? #

Magic Potion defines a _data_ model, it is not object-oriented
and does not deal with behavior at all. Data processing is done with
Clojure functions that are independent of data model and vice versa.

For you object-oriented lovers: **[polymorphism is fully supported](Polymorphism.md)** and even
much more powerful than in Java or other mainstream OO languages.
Clojure [multimethods](http://clojure.org/multimethods) can dispatch not only
based on the type of your objects (in this case, concepts of individuals)
but on any arbitrary function on the value of the individual.

Of course, Magic Potion statements can hold functions, so technically, you'd
even be able to add behavior to individuals. Beware, this is something
that we do not think goes well with the design goals of Magic Potion.

# Example 3: Relationships - connections between things #

![http://wiki.magicpotion.googlecode.com/hg/figures/mp-example-03-ref-one.png](http://wiki.magicpotion.googlecode.com/hg/figures/mp-example-03-ref-one.png)