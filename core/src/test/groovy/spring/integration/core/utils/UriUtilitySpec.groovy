package spring.integration.core.utils

import spock.lang.Specification
import spock.lang.Unroll

class UriUtilitySpec extends Specification {

    @Unroll
    def "createUriString #test"() {
        expect:
        UriUtility.createUriString(baseUri, pathList) == exptected

        where:
        test                                    | baseUri | pathList       || exptected
        "with base uri which does not have '/'" | "root"  | ["foo", "bar"] || "root/foo/bar"
        "with base uri which has '/'"           | "root/" | ["foo", "bar"] || "root/foo/bar"
        "with base uri with empty list"         | "root"  | []             || "root/"
    }

    @Unroll
    def "createUriWithReplacingPath #test"() {
        expect:
        UriUtility.createUriWithReplacingPath(originalUri, pathList) == exptected

        where:
        test                  | originalUri                            | pathList       || exptected
        "Test with http url"  | new URI("http://localhost:1234/test/") | ["foo", "bar"] || new URI("http://localhost:1234/foo/bar")
        "Test with no port"   | new URI("https://localhost/root/?foo=bar")  | ["foo", "bar"] || new URI("https://localhost/foo/bar?foo=bar")
        "Test with no schema" | new URI("hoge")                        | []             || new URI("/")
    }

}
