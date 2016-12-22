import * as React from "react";
import * as ReactDOM from "react-dom";
import {SearchPage} from "./SearchPage";
import {App} from "./AppPage";
import { Router, Link, Route, browserHistory, IndexRoute } from 'react-router'

const Home = () => {
  return (
    <div id="user-wrapper">
      <div className="user-info">
        <div className="name">
          <h1>
            Не зволікай!
          </h1>
        </div>
        <div className="props">
          <h2>
            Знайди справу до душі
          </h2>
        </div>
        <div>
          <a className="know-more" href="search">Пошук</a>
        </div>
      </div>
    </div>
  )
};

ReactDOM.render((
  <Router history={browserHistory}>
    <Route path="/" component={App}>
      <IndexRoute component={Home}/>
      <Route path="search" component={SearchPage}/>
    </Route>
  </Router>
), document.getElementById('root'));
