package book.kotlin.ch3

data class HttpResponse(val body: String)

data class ParseHtmlArticle(val uri: String, val htmlString: String)

data class ParseArticle(val url: String)

data class ArticleBody(val uri: String, val body: String)