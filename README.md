JavaConfig
==========

xml config reader.

Config example :
`
<?xml version="1.1" encoding="UTF-8" ?>
<config>
    <param name="language" value="ru" />
    <include file="lang/lang.!language!.xml" />
</config>
`

lang/lang.ru.xml :
`
<?xml version="1.1" encoding="UTF-8" ?>
<config>
    <param name="LANG_SPLIT_REGEXP" value="[^(рублей)] |-|_|:|!|[^0123456789]\.|\.[^0123456789]" />
    <param name="PRICE_EXTRACTION_REGEXP" value="(^([0-9]+)|([0-9]+((\.|рублей| )+)[0-9]+)$)" />
    <param name="PRICE_EXTRACTION_DELIMETER" value="\.|руб|рублей| " />
    <param name="PRICE_EXTRACTION_TRASH" value="лей| |коп|копеек" />
    <param name="check" value="!language!" />
</config>
`

Usage :
HashMap<String, String> config = Config.LoadConfig(Paths.get("config.xml"));
