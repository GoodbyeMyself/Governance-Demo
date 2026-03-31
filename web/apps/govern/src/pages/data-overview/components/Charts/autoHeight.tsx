import React from 'react';

function computeHeight(node: HTMLDivElement) {
  const { style } = node;
  style.height = '100%';
  const totalHeight = parseInt(`${getComputedStyle(node).height}`, 10);
  const padding =
    parseInt(`${getComputedStyle(node).paddingTop}`, 10) +
    parseInt(`${getComputedStyle(node).paddingBottom}`, 10);
  return totalHeight - padding;
}

function getAutoHeight(node: HTMLDivElement | null) {
  if (!node) {
    return 0;
  }

  let height = computeHeight(node);
  const parentNode = node.parentNode as HTMLDivElement | null;
  if (parentNode) {
    height = computeHeight(parentNode);
  }

  return height;
}

type AutoHeightProps = {
  height?: number;
};

function autoHeight() {
  return <P extends AutoHeightProps>(
    WrappedComponent: React.ComponentClass<P> | React.FC<P>,
  ): React.ComponentClass<P> => {
    class AutoHeightComponent extends React.Component<P & AutoHeightProps> {
      state = {
        computedHeight: 0,
      };

      root: HTMLDivElement | null = null;

      componentDidMount() {
        const { height } = this.props;
        if (!height && this.root) {
          let computedHeight = getAutoHeight(this.root);
          this.setState({ computedHeight });
          if (computedHeight < 1) {
            computedHeight = getAutoHeight(this.root);
            this.setState({ computedHeight });
          }
        }
      }

      handleRoot = (node: HTMLDivElement | null) => {
        this.root = node;
      };

      render() {
        const { height } = this.props;
        const { computedHeight } = this.state;
        const resolvedHeight = height || computedHeight;
        return (
          <div ref={this.handleRoot}>
            {resolvedHeight > 0 && (
              <WrappedComponent {...this.props} height={resolvedHeight} />
            )}
          </div>
        );
      }
    }

    return AutoHeightComponent;
  };
}

export default autoHeight;
