import * as React from "react";
import * as _ from "lodash";

import {
  SearchkitManager, SearchkitProvider,
  SearchBox, RefinementListFilter, MenuFilter,
  Hits, HitsStats, NoHits, Pagination, SortingSelector,
  SelectedFilters, ResetFilters, ItemHistogramList,
  Layout, LayoutBody, LayoutResults, TopBar,
  SideBar, ActionBar, ActionBarRow, DynamicRangeFilter, RangeFilter,
  ImmutableQuery, SimpleQueryString, CheckboxFilter, RangeQuery
} from "searchkit";

require("./index.scss");

const host = "http://localhost:1337/localhost:9200/vstup_alias";

const searchkit = new SearchkitManager(host);

searchkit.translateFunction = (key) => {
  let translations = {
    "daily": "Очна",
    "extra": "Заочна",
    "night": "Вечірня",
    "facets.view_more": "Більше варіантів",
    "facets.view_less": "Менше варіантів",
    "facets.view_all": "Усі варіанти",
    "reset.clear_all": "Очистити фільтри",
    "NoHits.NoResultsFound": "Не знайдено результатів за запитом {query}.",
    "NoHits.DidYouMean": "Можливо, ви мали на увазі {query}.",
    "NoHits.SearchWithoutFilters": "Шукати за запитом {query} без фільтрів.",
    "NoHits.NoResultsFoundDidYouMean": "Не знайдено результатів за запитом {query}. Можливо, ви мали на увазі {suggestion}?",
    "hitstats.results_found": "Кількість спеціальностей: {hitCount}",
    "pagination.previous": "Назад",
    "pagination.next": "Вперед"
  };
  return translations[key]
};
function ListItem(props) {
  return <li>{props.value.name} ({props.value.k})</li>;
}

var useMarks = false;
var userMarks = null;

searchkit.setQueryProcessor((plainQueryObject)=> {
  if (useMarks && userMarks != null) {
    plainQueryObject.fields = ["_source"];
    plainQueryObject.query = {
      "bool": {
        "filter": {
          "script": {
            "script": "(_source.main.find{obj -> obj.name == main1Name}?.k ?: 0) * main1 + (_source.main.find{obj -> obj.name == main2Name}?.k ?: 0) * main2 + (_source.optional[0]?.k ?: 0) * opt + (_source.certificateK ?: 0) * cert > _source.stat.min",
            "params": {
              "main1Name": userMarks.main1Subject,
              "main1": userMarks.main1,
              "main2Name": userMarks.main2Subject,
              "main2": userMarks.main2,
              "opt": userMarks.optional,
              "cert": userMarks.certificate
            }
          }
        }
      }
    };
    plainQueryObject.script_fields = {
      "mark": {
        "script": {
          "inline": "(_source.main.find{obj -> obj.name == main1Name}?.k ?: 0) * main1 + (_source.main.find{obj -> obj.name == main2Name}?.k ?: 0) * main2 + (_source.optional[0]?.k ?: 0) * opt + (_source.certificateK ?: 0) * cert",
          "params": {
            "main1Name": "українська мова та література",
            "main1": 170,
            "main2Name": "математика",
            "main2": 160,
            "opt": 180,
            "cert": 190
          }
        }
      }
    };
  }
  return plainQueryObject
});

const SpecialityHitsGridItem = (props)=> {
  const {bemBlocks, result} = props;
  const source:any = _.extend({}, result._source, result.highlight);
  const res = result._source;
  var mark = 0;
  if (result.fields != null && result.fields.mark != null) {
    mark = Math.floor(result.fields.mark);
  }
  var universityLink = res.university.site;
  if (!universityLink.startsWith("http")) {
    universityLink = "http://" + universityLink;
  }
  return (
    <div className={bemBlocks.item().mix(bemBlocks.container("item"))} data-qa="hit">
      <div className={bemBlocks.item("details")}>
        <h2 className={bemBlocks.item("title")}>{res.name}</h2>
        <h2 className={bemBlocks.item("subtitle")}>Факультет: {res.faculty}</h2>
        <div className={bemBlocks.item("text")}><a href={universityLink} target="_blank">{res.university.name}</a></div>
      </div>
      <div className={bemBlocks.item("poster")}>
        <div>Прохідний бал: {parseFloat(res.stat.min.toFixed(2))}</div>
        <div>Бюджетний бал: {parseFloat(res.stat.freeMax.toFixed(2))}</div>
        <div>Вищий бал: {parseFloat(res.stat.max.toFixed(2))}</div>
        <div>Ваша оцінка: {parseFloat(mark.toFixed(2))}</div>
      </div>
      <div className={bemBlocks.item("poster")}>
        <div>Кількість місць: {res.total}</div>
        <div>Бюджетних місць: {res.free}</div>
      </div>
      <div>
        <ul className="subjects">{res.main.map((e,index)=> <ListItem key={e.name} value={e}/>)}</ul>
        <ul className="subjects">{res.optional.map((e,index)=> <ListItem key={e.name} value={e}/>)}</ul>
        <div style={{ marginLeft: 30}}>Атестат: {parseFloat(res.certificateK.toFixed(2))}</div>
      </div>

    </div>
  )
};

export class SearchPage extends React.Component {
  render() {
    return (
      <SearchkitProvider searchkit={searchkit}>
        <Layout>
          <TopBar>
            <SearchBox
              autofocus={true}
              searchOnChange={true}
              placeholder="Пошук..."
              prefixQueryFields={["name^10","faculty^5","university.name"]}/>
          </TopBar>
          <LayoutBody>
            <SideBar>
              <MarkForm/>
              <RefinementListFilter
                id="region"
                title="Регіон"
                field="region.raw"
                operator="OR"
                size={10}/>
              <RefinementListFilter
                id="main"
                title="Обов'язковий предмет"
                field="main.name.raw"
                operator="AND"
                size={7}/>
              <RefinementListFilter
                id="optional"
                title="Опціональний предмет"
                field="optional.name.raw"
                operator="OR"
                size={7}/>
              <RefinementListFilter
                id="type"
                title="Тип навчального закладу"
                field="university.type.raw"
                operator="OR"
                size={5}/>
              <RefinementListFilter
                id="timing"
                title="Форма навчання"
                field="timing"
                operator="OR"
                size={5}/>
              <CheckboxFilter id="budget" title="Бюджетні містя" label="Присутні"
                              filter={RangeQuery("free", {gt: 0})}/>
            </SideBar>
            <LayoutResults>
              <ActionBar>
                <ActionBarRow>
                  <HitsStats/>
                  <SortingSelector options={[
										{label:"Релевантність", field:"_score", order:"desc", defaultOption:true},
										{label:"Зменшення прохідного балу", field:"stat.min", order:"desc"},
										{label:"Зростання прохідного балу", field:"stat.min", order:"asc"}
									]}/>
                </ActionBarRow>
                <ActionBarRow>
                  <SelectedFilters/>
                  <ResetFilters/>
                </ActionBarRow>
              </ActionBar>
              <Hits mod="sk-hits-list" hitsPerPage={10} itemComponent={SpecialityHitsGridItem}/>
              <NoHits/>
              <Pagination showNumbers={true}/>
            </LayoutResults>
          </LayoutBody>
        </Layout>
      </SearchkitProvider>
    )
  }
}

class MarkForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      main1Subject: 'українська мова та література',
      main1: 0,
      main2Subject: 'математика',
      main2: 0,
      optional: 0,
      certificate: 0
    };

    this.main1Change = this.main1Change.bind(this);
    this.main1SubjectChange = this.main1SubjectChange.bind(this);
    this.main2Change = this.main2Change.bind(this);
    this.main2SubjectChange = this.main2SubjectChange.bind(this);
    this.optionalChange = this.optionalChange.bind(this);
    this.certificateChange = this.certificateChange.bind(this);
    this.toggleCheckbox = this.toggleCheckbox.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  main1SubjectChange(event) {
    this.setState({main1Subject: event.target.value});
  }

  main1Change(event) {
    this.setState({main1: parseFloat(event.target.value)});
  }

  main2SubjectChange(event) {
    this.setState({main2Subject: event.target.value});
  }

  main2Change(event) {
    this.setState({main2: parseFloat(event.target.value)});
  }

  optionalChange(event) {
    this.setState({optional: parseFloat(event.target.value)});
  }

  handleSubmit(event) {
    console.log(this.state);
    userMarks = Object.assign({}, this.state);
    searchkit.reloadSearch();
    event.preventDefault();
  }

  certificateChange(event) {
    var certificate = parseFloat(event.target.value) / 12.0 * 200.0;
    this.setState({certificate: certificate});
  }

  toggleCheckbox(event) {
    useMarks = !useMarks;
    searchkit.reloadSearch();
  }

  render() {
    return (
      <form onSubmit={this.handleSubmit}>
        <div>
          <label className="markLabel" htmlFor="main1subject">Обов'язковий предмет 1:</label>
          <div className="sk-select">
            <select id="main1subject" value={this.state.main1Subject} onChange={this.main1SubjectChange}>
              <option value="українська мова та література">українська мова та література</option>
              <option value="математика">математика</option>
              <option value="історія україни">історія україни</option>
              <option value="творчий конкурс">творчий конкурс</option>
              <option value="біологія">біологія</option>
              <option value="географія">географія</option>
              <option value="фізика">фізика</option>
              <option value="хімія">хімія</option>
              <option value="англійська мова">англійська мова</option>
            </select>
          </div>
        </div>
        <div style={{marginTop: 7}}>
          <input className="markInput" type="number" id="main1" min="0" max="200" step="0.1" onChange={this.main1Change}
                 required="true"/>
        </div>
        <div>
          <label className="markLabel" htmlFor="main2subject">Обов'язковий предмет 2:</label>
          <div className="sk-select">
            <select id="main2subject" value={this.state.main2Subject} onChange={this.main2SubjectChange}>
              <option value="українська мова та література">українська мова та література</option>
              <option value="математика">математика</option>
              <option value="історія україни">історія україни</option>
              <option value="творчий конкурс">творчий конкурс</option>
              <option value="біологія">біологія</option>
              <option value="географія">географія</option>
              <option value="фізика">фізика</option>
              <option value="хімія">хімія</option>
              <option value="англійська мова">англійська мова</option>
            </select>
          </div>
        </div>
        <div style={{marginTop: 7}}>
          <input className="markInput" type="number" id="main2" min="0" max="200" step="0.1" onChange={this.main2Change}
                 required="true"/>
        </div>
        <div>
          <label className="markLabel" htmlFor="optional">Опціональний предмет:</label>
          <input className="markInput" type="number" id="optional" min="0" max="200" step="0.1"
                 onChange={this.optionalChange}
                 required="true"/>
        </div>
        <div>
          <label className="markLabel" htmlFor="certificate">Атестат:</label>
          <input className="markInput" type="number" id="certificate" min="0" max="12" step="0.1"
                 onChange={this.certificateChange}
                 required="true"/>
        </div>
        <div>
          <input className="markButton" type="submit" value="Шукати"/>
          <label htmlFor="check">Примінити:</label>
          <input type="checkbox" id="check" onChange={this.toggleCheckbox}/>
        </div>
      </form>
    );
  }
}
