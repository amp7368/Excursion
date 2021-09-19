# Excursion

# API
<div id="ping_endpoint">
  <h3>
    Ping
  </h3>
  <p>
    <b> Endpoint:</b>
    <a href='http://appleptr16.com:8080/excursion/ping'>
      /excursion/ping
    </a>
    <br>
    <b>Description</b>
    A simple test request
    <br>
    <b>Response:</b>
  </p>
  <pre>
  <code class="lang-js">
   pong
  </code>
</pre>
</div>
<div id="validate_endpoint">
  <h3>
    Validate
  </h3>
  <p>
    <b> Endpoint:</b>
    <a href='http://appleptr16.com:8080/excursion/validate'>
      /excursion/validate
    </a>
    <br>
    <b>Parameters: </b>
    <br>
    &nbsp <em>accessToken:</em> the accessToken provided from the mod
    <br>
    &nbsp <em>clientToken:</em> the clientToken provided from the mod
    <br>
    <b>Description</b>
    Validates the accessToken and clientToken from the mod to create a YinSessionId
    <br>
    <b>Response:</b>
  </p>
  <pre>
  <code class="lang-js">
    ```
    "session": %int%
    ````
  </code>
  </pre>
</div>
<div id="task_all_endpoint">
  <h3>
    All Tasks
  </h3>
  <p>
    <b> Endpoint:</b>
    <a href='http://appleptr16.com:8080/excursion/task/names/all'>
      /excursion/task/all
    </a>
    <br>
    <b>Parameters: </b>
    <br>
    &nbsp <em>session:</em> the session id you got earlier
    <br>
    <b>Description</b>
    Gets all the task names
    <br>
    <b>Response:</b>
  </p>
  <pre>
  <code class="lang-js">
    [
      ```
      "name": %String%
      ````,
      ...
    ]
  </code>
</pre>
</div>
<div id="task_get_single">
  <h3>
    Get Task
  </h3>
  <p>
    <b> Endpoint:</b>
    <a href='http://appleptr16.com:8080/excursion/task/get'>
      /excursion/task/get
    </a>
    <br>
    <b>Parameters: </b>
    <br>
    &nbsp <em>session:</em> the session id you got earlier
    <br>
    &nbsp <em>name:</em> the name (or part) of the task
    <br>
    <b>Description</b>
    Gets a <b>single</b> task matching the predicate or null when there is no match
    <br>
    <b>Response:</b>
  </p>
  <pre>
  <code class="lang-js">
    [
      ```
        "name": "Komunus",
        "category": "mission",
        "description": "Help a Wynncraftian kill any of these bosses:\nBob, Corrupter of Worlds, Death, Qira & Orange Wybel",
        "coordinates": "Mission",
        "bulletsCount": 5,
        "creationDate": "",
        "points": 100,
        "createdBy": "Fox",
        "images": "https://imgur.com/Dj2YE4u.png"
      ````...
    ]
  </code>
</pre>
</div>
<div id="task_submit_endpoint">
  <h3>
    Submit
  </h3>
  <p>
    <b> Endpoint:</b>
    <a href='http://appleptr16.com:8080/excursion/task/submit'>
      /excursion/task/submit
    </a>
    <br>
    <b>Parameters: </b>
    <br>
    &nbsp <em>session:</em> the session id you got earlier
    <br>
    &nbsp <em>name:</em> the name (or part) of the task
    <br>
    <b>Description</b>
    Gets a <b>single</b> task matching the predicate or null when there is no match
    <br>
    <b>Response:</b>
  </p>
  <pre>
  <code class="lang-js">
    [
      ```
        "name": "Komunus",
        "category": "mission",
        "description": "Help a Wynncraftian kill any of these bosses:\nBob, Corrupter of Worlds, Death, Qira & Orange Wybel",
        "coordinates": "Mission",
        "bulletsCount": 5,
        "creationDate": "",
        "points": 100,
        "createdBy": "Fox",
        "images": "https://imgur.com/Dj2YE4u.png"
      ````...
    ]
  </code>
</pre>
</div>
