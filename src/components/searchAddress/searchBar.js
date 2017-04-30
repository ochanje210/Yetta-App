import React, { Component, PropTypes } from 'react';
import {
  TextInput,
  View,
  Keyboard,
  Dimensions,
  Text,
  TouchableOpacity,
  ListView
} from 'react-native';
import { APIKEY } from './../../utils';
import * as GOOGLE_MAPS_API from './../../service/GoogleMapsAPI';

const HEIGHT = Dimensions.get('window').height;
const WIDTH = Dimensions.get('window').width;

export default class SearchBar extends Component {
  constructor() {
    super();
    const ds = new ListView.DataSource({rowHasChanged: (r1, r2) => r1 !== r2});
    this.state = {
      onFocused: false,
      listViewDataSource: ds.cloneWithRows([])
    };
    this.renderListView = this.renderListView.bind(this);
  }

  renderRow(rowData) {
    let arr = [];
    const { terms } = rowData;
    terms && terms.map((term, i) => {
      if (i > 0) {
        arr.push(
          <Text
            key={i}
            style={{
              fontSize: 10,
              color: 'grey',
              marginRight: 5
            }}
          >
            {term.value}
          </Text>
        );
      }
    });

    let textHead;
    if (rowData.first) {
      textHead = rowData.first;
    } else if (rowData.last) {
      textHead = rowData.last;
    } else if (terms) {
      textHead = terms[0].value;
    }

    return (
      <TouchableOpacity
        style={{
          height: 50,
          width: WIDTH,
          borderBottomWidth: 1,
          borderBottomColor: '#f7f9f9',
          justifyContent: 'center',
          paddingLeft: 40
        }}
        onPress={() => {
          this.setState({onFocused: false});
          if (terms) {
            // when predicted address clicked

            this.props.setBusyWaitingPlaceDetailAPI(true);

            // search the predicted address in detail
            GOOGLE_MAPS_API.placeDetails(rowData.place_id)
              .then((res) => {
                this.props.setBusyWaitingPlaceDetailAPI(false);

                // res: coordinate of predicted place
                // keys: lat, lon
                this.props.handleAddressBtn(terms[0].value, terms.slice(1).map(e => e.value + ' '), res);
              });
          } else {
            // when 현재 내 위치/핀으로 찾기 clicked
            this.props.handleAddressBtn('현재 내 위치', []);
          }
        }}
      >
        <Text
          style={{
            color: '#303233'
          }}
          numberOfLines={1}
        >
          {textHead}
        </Text>
        {(arr.length === 0) ? null :
          <View style={{
            height: 20,
            width: WIDTH,
            flexDirection: 'row',
            alignItems: 'center'
          }}>
            {arr}
          </View>
        }
      </TouchableOpacity>
    );
  }

  renderListView() {
    return (
      <ListView
        dataSource={this.state.listViewDataSource}
        renderRow={(rowData) => this.renderRow(rowData)}
        style={{flex: 1}}
        enableEmptySections
      />
    );
  }

  handleTextChange(text) {
    const AUTOCOMPLETEURL = `https://maps.googleapis.com/maps/api/place/autocomplete/json?input=${text}` +
      `&location=${this.props.latitude},${this.props.longitude}&radius=500&key=${APIKEY}`;
    this.setState({
      text: text
    });
    const arr = [];
    arr.push({first: '내 위치'});
    fetch(AUTOCOMPLETEURL, {method: 'GET'})
      .then(response => response.json())
      .then(rjson => {
        const { status } = rjson;
        if (status === 'OK') {
          return rjson.predictions;
        }
        throw new Error(rjson.error_message);
      })
      .then(predictions => {
        predictions.map(place => {
          arr.push(place);
        });
        arr.push({last: '핀으로 찾기'});
        this.setState({listViewDataSource: this.state.listViewDataSource.cloneWithRows(arr)});
      })
      .catch(console.log);
  }

  render() {
    const { onFocused } = this.state;
    const SEARCHBAR_HEIGHT = 50;

    let topVal = 100;
    if (this.props.onDelivery && this.props.isRunner) {
      topVal = -60;
    } else if (onFocused === true) {
      topVal = 0;
    }
    return (
      <View
        style={{
          position: 'absolute',
          left: onFocused ? 0 : (WIDTH - WIDTH * 0.8) / 2,
          top: topVal,
          width: onFocused ? WIDTH : WIDTH * 0.8,
          height: onFocused ? HEIGHT : SEARCHBAR_HEIGHT,
          backgroundColor: 'white',
          elevation: 60,
          zIndex: 3
        }}
      >
        <View style={{
          position: 'absolute',
          left: 0,
          top: 0,
          width: onFocused ? WIDTH : WIDTH * 0.8,
          height: onFocused ? HEIGHT : SEARCHBAR_HEIGHT,
          backgroundColor: 'white',
          shadowOffset: {height: 1, width: 1},
          shadowOpacity: 0.2,
          flexDirection: onFocused ? 'column' : 'row'
        }}>
          <TouchableOpacity
            style={{
              flex: 1,
              backgroundColor: 'white',
              shadowOffset: {height: 1, width: 1},
              shadowOpacity: 0.2,
              justifyContent: 'space-between',
              paddingLeft: 17,
              paddingRight: 20,
              paddingTop: 20,
              flexDirection: 'row'
            }}
            onPress={() => {
              // only work when not focused
              if (!onFocused) {
                this.setState({onFocused: true});
                this.props.setSearchBarExpanded(true);
              }
            }}
            activeOpacity={(onFocused) ? 1 : 0.5}
          >
            {(onFocused) ? null :
              <Text style={{
                alignSelf: 'center',
                color: '#979797',
                top: -9,
                fontSize: 16
              }}>어디로 배달하시겠어요?</Text>}
            <View style={{
              width: 30,
              height: 20,
              alignSelf: 'center',
              marginTop: 5
            }}>
              {(onFocused) ?
                <TouchableOpacity onPress={() => {
                  this.setState({onFocused: false});
                  this.props.setSearchBarExpanded(false);
                  Keyboard.dismiss();
                }}>
                  <Text style={{fontSize: 10}}>back</Text>
                </TouchableOpacity>
                : null}
            </View>
            {(onFocused) ?
              <TextInput
                style={(onFocused) ? {
                  height: 40,
                  width: WIDTH * 0.8,
                  backgroundColor: '#f9f9f9',
                  alignSelf: 'center',
                  borderRadius: 4,
                  paddingLeft: 10
                } : {height: 40, paddingLeft: 10}}
                onChangeText={this.handleTextChange.bind(this)}
                value={this.state.text}
                underlineColorAndroid={'white'}
              />
              : null}
          </TouchableOpacity>
          {(onFocused) ?
            <View style={{flex: 6.5}}>
              {this.renderListView()}
            </View>
            : null}
        </View>
      </View>
    );
  }
}

SearchBar.propTypes = {
  latitude: PropTypes.any,
  longitude: PropTypes.any,
  handleAddressBtn: PropTypes.func.isRequired,
  setBusyWaitingPlaceDetailAPI: PropTypes.func.isRequired,
  setSearchBarExpanded: PropTypes.func.isRequired,
  onDelivery: PropTypes.bool.isRequired,
  isRunner: PropTypes.bool.isRequired
};
